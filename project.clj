(defproject clj-hypermq "0.0.1"
  :description "Clojure client for hypermq"
  :url "http://github.com/uswitch/clj-hypermq"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.1"]]
  :profiles {:dev {:dependencies [[midje "1.5.1"]]}})
