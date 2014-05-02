(ns clj-hypermq.client-test
  (:require [midje.sweet        :refer :all]
            [clj-hypermq.client :refer :all]
            [clj-http.client    :as http]))

(fact "create message"
      (create-message "http://server" "myqueue" "myproducer" {:foo "bar"}) => anything
      (provided
       (http/post "http://server/q/myqueue" {:form-params {:producer "myproducer"
                                                           :body {:foo "bar"}}
                                             :content-type :json}) => anything))

(fact "200 status is a successful create message"
      (create-message "http://server" "myqueue" "myproducer" {:foo "bar"}) => true?
      (provided
       (http/post anything anything) => {:status 201}))

(fact "non 200 status is an unsuccessful create message"
      (create-message "http://server" "myqueue" "myproducer" {:foo "bar"}) => false?
      (provided
       (http/post anything anything) => {:status 404}))

(fact "fetch messages from queue uri"
      (fetch-messages "http://server" "myqueue") => anything
      (provided
       (http/get "http://server/q/myqueue" {:as :json}) => anything))

(fact "fetches a single message"
      (fetch-messages "http://server" "myqueue") => [{:id "1" :producer "author" :created 54321}]
      (provided
       (http/get anything anything) => {:body {:_embedded
                                               {:message [{:id "1" :producer "author" :created 54321}]}}}))

(fact "will crawl next links when present"
      (fetch-messages "http://server" "myqueue") => anything
      (provided
       (http/get "http://server/q/myqueue" anything) => {:body {:_links {:next {:href "http://server/q/myqueue/1"}}}}
       (http/get "http://server/q/myqueue/1" anything) => {:body {:_links {:next {:href "http://server/q/myqueue/2"}}}}
       (http/get "http://server/q/myqueue/2" anything) => {:body {:_links {}}}))

(fact "will fetch messages across pages"
      (fetch-messages "http://server" "myqueue") => [{:uuid "1"} {:uuid "2"} {:uuid "3"}]
      (provided
       (http/get "http://server/q/myqueue" anything) => {:body {:_links {:next {:href "http://server/q/myqueue/1"}}
                                                                :_embedded {:message [{:uuid "1"}]}}}
       (http/get "http://server/q/myqueue/1" anything) => {:body {:_links {:next {:href "http://server/q/myqueue/2"}}
                                                                  :_embedded {:message [{:uuid "2"}]}}}
       (http/get "http://server/q/myqueue/2" anything) => {:body {:_links {}
                                                                  :_embedded {:message [{:uuid "3"}]}}}))

(fact "should fetch up to last known etag"
      (fetch-messages "http://server" "myqueue" :etag "2") => [{:uuid "3"} {:uuid "4"} {:uuid "5"}]
      (provided
       (http/get "http://server/q/myqueue/2" anything) => {:body {:_links {:next {:href "http://server/q/myqueue/3"}}
                                                                :_embedded {:message [{:uuid "3"} {:uuid "4"}]}}}
       (http/get "http://server/q/myqueue/3" anything) => {:body {:_links {}
                                                                  :_embedded {:message [{:uuid "5"}]}}}))

(fact "client acknowledges a message"
      (acknowledge "http://server" "myqueue" "myclient" "uuid12345") => true?
      (provided
       (http/post "http://server/ack/myqueue/myclient" {:form-params {:id "uuid12345"}
                                                        :content-type :json}) => {:status 201}))

(fact "client retrieves last seen message for a queue"
      (last-seen-message "http://server" "myqueue" "myclient") => "uuid-54321"
      (provided
       (http/get "http://server/ack/myqueue/myclient" anything) => {:body {:message "uuid-54321"}}))
