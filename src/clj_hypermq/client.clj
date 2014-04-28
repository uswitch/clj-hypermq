(ns clj-hypermq.client
  (:require [clj-http.client :as http]))

(defn- build-uri [host queue]
  (str host "/q/" queue))

(defn create-message
  [host queue title author content]
  (let [msg {:title title :author author :content content}
        response (http/post (build-uri host queue) {:form-params msg :content-type :json})]
    (case (response :status)
      200 true
      false)))

(defn lazy-messages
  [uri]
  (when uri
      (let [response (http/get uri {:as :json})
            messages (get-in response [:body :_embedded :message])
            prev-page (get-in response [:body :_links :prev :href])]
        (lazy-cat messages (lazy-messages prev-page)))))

(defn fetch-messages
  [host queue]
  (lazy-messages (build-uri host queue)))
