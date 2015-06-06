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
package languagecheck

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"code.google.com/p/gcfg"

	"appengine"
)

var (
	cfg struct {
		APP struct {
			Key string
		}
	}
)

func init() {

	if err := gcfg.ReadFileInto(&cfg, "config/config.gcfg"); err != nil {
		log.Fatalf("gcfg.ReadFileInfo => %v", err.Error())
	}

	http.HandleFunc("/check", checkHandler)
	http.HandleFunc("/installed", installHandler)
}

// Handles the request for installer that Atlassian plugin requires.
func installHandler(w http.ResponseWriter, r *http.Request) {
	sendJSON(w, "")
}

// Handles the actuall API. All requests for results go through this.
func checkHandler(w http.ResponseWriter, r *http.Request) {
	c := appengine.NewContext(r)
	q := r.FormValue("q")
	target := r.FormValue("target")

	if len(target) == 0 {
		target = defaultTarget
	}

	result, err := Detect(q, target, c)

	if err != nil {
		handleError(w, err)
	}

	b, err := json.Marshal(result)
	if err != nil {
		handleError(w, err)
	}

	sendJSON(w, string(b))

}

// Ensures that all http errors are handled properly
func handleError(w http.ResponseWriter, err error) {
	http.Error(w, err.Error(), http.StatusInternalServerError)
}

// Takes care of sending proper JSON back to requests.
func sendJSON(w http.ResponseWriter, content string) {
	w.Header().Set("Content-Type", "application/json")
	w.Header().Set("X-Programming-Langauge", "go")
	fmt.Fprint(w, content)
}
