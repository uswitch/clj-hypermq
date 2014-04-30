# clj-hypermq

A Clojure client library for [hypermq][1]

[1]:https://github.com/uswitch/hypermq

## Usage

```clojure
(:require [hypermq.client :as client])

; Create messages on a queue
(client/create-message "http://localhost" "myqueue" "my-event" "producer1" {:msg "1"})
; true
(client/create-message "http://localhost" "myqueue" "my-event" "producer1" {:msg "2"})
; true

; Consume messages from a queue
(client/fetch-messages "http://localhost" "myqueue")
; [{:uuid "f83d9e11-58ed-446e-bc78-461f60b6189f" :timestamp 1398728082 :title "my-event" :author "producer1" :content {:msg "1"}} 
;  {:uuid "40d9141d-7d5e-445c-b9ce-4be2e7d93da3" :timestamp 1398729082 :title "my-event" :author "producer1" :content {:msg "2"}}]

; Optionally provide uuid of last seen message as etag to fetch only more recent messages 
(client/fetch-messages "http://localhost" "myqueue" :etag "f83d9e11-58ed-446e-bc78-461f60b6189f") 
; [{:uuid "40d9141d-7d5e-445c-b9ce-4be2e7d93da3" :timestamp 1398729082 :title "my-event" :author "producer1" :content {:msg "2"}}]

; Acknowledge message as seen by it's uuid
(acknowledge "http://localhost" "myqueue" "client1" "f83d9e11-58ed-446e-bc78-461f60b6189f")
; true

; Fetch the last seen message uuid for a client & queue
(last-seen-message "http://localhost" "myqueue" "myclient1")
; "f83d9e11-58ed-446e-bc78-461f60b6189f" 
```

## License

Copyright © 2014 Christian Blunden

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
