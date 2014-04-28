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
