(ns clj-hypermq.client-test
  (:require [midje.sweet        :refer :all]
            [clj-hypermq.client :refer :all]
            [clj-http.client    :as http]))

(fact "create message"
      (create-message "http://server" "myqueue" "mytitle" "author" {:foo "bar"}) => anything
      (provided
       (http/post "http://server/q/myqueue" {:form-params {:title "mytitle"
                                                           :author "author"
                                                           :content {:foo "bar"}}
                                             :content-type :json}) => anything))

(fact "200 status is a successful create message"
      (create-message "http://server" "myqueue" "mytitle" "author" {:foo "bar"}) => true?
      (provided
       (http/post anything anything) => {:status 200}))

(fact "non 200 status is an unsuccessful create message"
      (create-message "http://server" "myqueue" "mytitle" "author" {:foo "bar"}) => false?
      (provided
       (http/post anything anything) => {:status 404}))

(fact "fetch messages from queue uri"
      (fetch-messages "http://server" "myqueue") => anything
      (provided
       (http/get "http://server/q/myqueue" {:as :json}) => anything))

(fact "fetches a single message"
      (fetch-messages "http://server" "myqueue") => [{:uuid "1" :title "mytitle" :author "author" :timestamp 54321}]
      (provided
       (http/get anything anything) => {:body {:_embedded
                                               {:message [{:uuid "1" :title "mytitle" :author "author" :timestamp 54321}]}}}))

(fact "will crawl prev links when present"
      (fetch-messages "http://server" "myqueue") => anything
      (provided
       (http/get "http://server/q/myqueue" anything) => {:body {:_links {:prev {:href "http://server/q/myqueue/1"}}}}
       (http/get "http://server/q/myqueue/1" anything) => {:body {:_links {:prev {:href "http://server/q/myqueue/0"}}}}
       (http/get "http://server/q/myqueue/0" anything) => {:body {:_links {}}}))

(fact "will fetch messages across pages"
      (fetch-messages "http://server" "myqueue") => [{:uuid "3"} {:uuid "2"} {:uuid "1"}]
      (provided
       (http/get "http://server/q/myqueue" anything) => {:body {:_links {:prev {:href "http://server/q/myqueue/1"}}
                                                                :_embedded {:message [{:uuid "3"}]}}}
       (http/get "http://server/q/myqueue/1" anything) => {:body {:_links {:prev {:href "http://server/q/myqueue/0"}}
                                                                  :_embedded {:message [{:uuid "2"}]}}}
       (http/get "http://server/q/myqueue/0" anything) => {:body {:_links {}
                                                                  :_embedded {:message [{:uuid "1"}]}}}))

(fact "should fetch up to last known etag"
      (fetch-messages "http://server" "myqueue" :etag "2") => [{:uuid "3"}]
      (provided
       (http/get "http://server/q/myqueue" anything) => {:body {:_links {:prev {:href "http://server/q/myqueue/1"}}
                                                                :_embedded {:message [{:uuid "3"}]}}}
       (http/get "http://server/q/myqueue/1" anything) => {:body {:_links {:prev {:href "http://server/q/myqueue/0"}}
                                                                  :_embedded {:message [{:uuid "2"}]}}}))
