(ns datastar.html
  (:require
   [charred.api :as charred]
   [ring.util.response :as response]
   [selmer.parser :as selmer]))

(def ^:private bufSize 1024)
(def read-json (charred/parse-json-fn {:async? false :bufsize bufSize}))

(defn render
  ([template-path]
   (render template-path {}))
  ([template-path data]
   (->
    (str "templates/" template-path)
    (selmer/render-file data)
    (response/response)
    (response/content-type "text/html")
    (response/charset "UTF-8"))))

(defn fragment
  ([template-path]
   (fragment template-path {}))
  ([template-path data]
   (->
    (str "templates/" template-path)
    (selmer/render-file data))))
