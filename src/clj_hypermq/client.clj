(ns clj-hypermq.client
  (:require [clj-http.client :as http]))

(defn create-message
  [host queue title author content]
  (let [uri (str host "/q/" queue)
        msg {:title title :author author :content content}
        result (http/post uri {:form-params msg :content-type :json})]
    (case (result :status)
      200 true
      false)))
