(ns clj-hypermq.client
  (:require [clj-http.client :as http]))

(defn- build-uri
  ([host queue]
     (format "%s/q/%s" host queue))
  ([host queue etag]
     (if etag
       (format "%s/q/%s/%s" host queue etag)
       (build-uri host queue))))

(defn- build-ack-uri [host queue client]
  (format "%s/ack/%s/%s" host queue client))

(defn- lazy-messages
  [uri]
  (when uri
      (let [response (http/get uri {:as :json})
            messages (get-in response [:body :_embedded :message])
            next-page (get-in response [:body :_links :next :href])]
        (lazy-cat messages (lazy-messages next-page)))))

(defn create-message
  [host queue producer body]
  (let [msg {:producer producer :body body}
        response (http/post (build-uri host queue) {:form-params msg :content-type :json})]
    (case (response :status)
      201 true
      false)))

(defn fetch-messages
  [host queue & {:keys [etag]}]
  (lazy-messages (build-uri host queue etag)))

(defn acknowledge
  [host queue client msg-id]
  (let [msg {:id msg-id}
        response (http/post (build-ack-uri host queue client) {:form-params msg :content-type :json})]
    (case (response :status)
      201 true
      false)))

(defn last-seen-message
 [host queue client]
 (let [response (http/get (build-ack-uri host queue client) {:as :json})]
   (get-in response [:body :message])))
