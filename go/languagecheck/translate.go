/*
	Copyright 2015, Google, Inc.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
// This file contains all of the logic to talk to Google Translate API and handle caching results.
package languagecheck

import (
	"encoding/json"
	"errors"
	"io/ioutil"
	"net/url"

	"appengine"
	"appengine/memcache"
	"appengine/urlfetch"
)

const (
	defaultTarget = "en"
	base          = "https://www.googleapis.com/language/translate/v2"
)

// The format for the list of all languages in a target language.
type langList struct {
	Data struct {
		Languages []struct {
			Language string
			Name     string
		}
	}
}

// Format for getting detection information out of Google Translate
type dList struct {
	Data struct {
		Detections [][]struct {
			Language string
		}
	}
}

// The code + target language version of the language we want to return to service
type Result struct {
	Data  string `json:"data"`
	Label string `json:"label"`
}

// Returns the human friendly label for a language, in the target language
func Label(lang, target string, c appengine.Context) (string, error) {
	key := "lang:" + target + ":" + lang

	item, err := memcache.Get(c, key)
	if err != nil && err != memcache.ErrCacheMiss {
		return "", err
	}

	var result string
	if err == memcache.ErrCacheMiss {

		lList, err := langFromAPI(target, c)
		if err != nil {
			return "", err
		}

		for key, value := range lList {
			k := "lang:" + target + ":" + key

			item := &memcache.Item{
				Key:   k,
				Value: []byte(value),
			}

			memcache.Set(c, item)
		}

		r, ok := lList[lang]
		if !ok {
			return "", errors.New("Language not supported")
		}
		result = string(r)

	} else {
		value := item.Value
		result = string(value)
	}

	return result, nil
}

// Returns the human friendly label and language code for the input string: s
func Detect(s, target string, c appengine.Context) (Result, error) {

	item, err := memcache.Get(c, s)
	if err != nil && err != memcache.ErrCacheMiss {
		return Result{"", ""}, err
	}

	var r string
	if err == memcache.ErrCacheMiss {
		r, err = detectFromAPI(s, c)
		if err != nil {
			return Result{"", ""}, err
		}
		item := &memcache.Item{
			Key:   s,
			Value: []byte(r),
		}

		memcache.Set(c, item)

	} else {
		value := item.Value
		r = string(value)
	}

	l, err := Label(r, target, c)
	if err != nil {
		return Result{"", ""}, err
	}

	result := Result{r, l}

	return result, nil
}

// Makes a call to Google Translate API to get the language of the input string
func detectFromAPI(s string, c appengine.Context) (string, error) {

	path := "/detect"

	var u *url.URL
	u, err := url.Parse(base + path)
	if err != nil {
		return "", err
	}

	parameters := url.Values{}
	parameters.Add("q", s)
	parameters.Add("key", cfg.APP.Key)
	u.RawQuery = parameters.Encode()

	client := urlfetch.Client(c)

	resp, err := client.Get(u.String())
	if err != nil {
		return "", err
	}
	body, err := ioutil.ReadAll(resp.Body)
	resp.Body.Close()
	if err != nil {
		return "", err
	}

	var d dList
	if err := json.Unmarshal(body, &d); err != nil {
		return "", err
	}

	return d.Data.Detections[0][0].Language, nil
}

// Makes a call to Google Translate API to get the list of human readable languages in the target language
func langFromAPI(target string, c appengine.Context) (map[string]string, error) {
	path := "/languages"
	q := "?target=" + target + "&key=" + cfg.APP.Key
	url := base + path + q
	client := urlfetch.Client(c)

	resp, err := client.Get(url)
	if err != nil {
		return nil, err
	}
	body, err := ioutil.ReadAll(resp.Body)
	resp.Body.Close()
	if err != nil {
		return nil, err
	}

	var l langList
	if err := json.Unmarshal(body, &l); err != nil {
		return nil, err
	}

	result := make(map[string]string)
	for _, value := range l.Data.Languages {
		result[value.Language] = value.Name
	}

	return result, nil
}
