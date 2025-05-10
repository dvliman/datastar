(ns datastar.html
  (:require
   [ring.util.response :as response]
   [selmer.parser :as selmer]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

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

(defmacro merge-fragment! [req & body]
  `(->sse-response
    ~req
    {on-open
     (fn [sse#]
       (d*/with-open-sse sse#
         (d*/merge-fragment! sse# ~@body)))}))

(defmacro merge-fragments! [req & body]
  `(->sse-response
    ~req
    {on-open
     (fn [sse#]
       (d*/with-open-sse sse#
         (d*/merge-fragments! sse# ~@body)))}))
