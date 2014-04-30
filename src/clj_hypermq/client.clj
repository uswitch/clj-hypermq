(ns clj-hypermq.client
  (:require [clj-http.client :as http]))

(defn- build-uri [host queue]
  (str host "/q/" queue))

(defn- build-ack-uri [host queue client]
  (str host "/ack/" queue "/" client))

(defn- lazy-messages
  [uri]
  (when uri
      (let [response (http/get uri {:as :json})
            messages (get-in response [:body :_embedded :message])
            prev-page (get-in response [:body :_links :prev :href])]
        (lazy-cat messages (lazy-messages prev-page)))))

(defn- not-matching
  [etag]
  (fn [msg] (not= (msg :uuid) etag)))

(defn create-message
  [host queue title author content]
  (let [msg {:title title :author author :content content}
        response (http/post (build-uri host queue) {:form-params msg :content-type :json})]
    (case (response :status)
      200 true
      false)))

(defn fetch-messages
  [host queue & {:keys [etag]}]
  (take-while (not-matching etag) (lazy-messages (build-uri host queue))))

(defn acknowledge
  [host queue client uuid]
  (let [msg {:uuid uuid}
        response (http/post (build-ack-uri host queue client) {:form-params msg :content-type :json})]
    (case (response :status)
      201 true
      false)))

(defn last-seen-message
 [host queue client]
 (let [response (http/get (build-ack-uri host queue client) {:as :json})]
   (get-in response [:body :uuid])))
