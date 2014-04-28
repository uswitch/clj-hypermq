# clj-hypermq

A Clojure client library for [hypermq][1]

[1]:https://github.com/uswitch/hypermq

## Usage

```clojure
(:require [hypermq.client :as client])

; Create a message
(client/create-message "http://localhost:3000" "myqueue" "title" "author" {:foo "bar"})
; true

; Consume messages
(client/fetch-messages "http://localhost" "myqueue")
; [{:uuid "f83d9e11-58ed-446e-bc78-461f60b6189f" :timestamp 1398728082 :title "title" :author "author" :content {:foo "bar"}}]

; Optionally provide last seen etag
(client/fetch-messages "http://localhost" "myqueue" :etag "f83d9e11-58ed-446e-bc78-461f60b6189f") 
; []
```

## License

Copyright © 2014 Christian Blunden

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
