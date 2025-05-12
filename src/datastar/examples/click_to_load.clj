(ns datastar.examples.click-to-load
  (:require
   [datastar.html :as html]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]])
  (:import [java.security MessageDigest]
           [java.nio ByteBuffer]))

(defn alias-hash [alias]
  (let [digest (MessageDigest/getInstance "SHA-256")
        bytes (.digest digest (.getBytes alias "UTF-8"))
        buffer (ByteBuffer/wrap bytes)
        long-val (.getLong buffer)]
    (bit-and long-val 0x7fffffffffffffff)))

(defn render [req]
  (html/render
   "click-to-load.html"
   {:signals {:limit 10 :offset 0}
    :limit 10
    :offset 0
    :limit-plus-offset (+ 10 0)}))

(defn more [req]
  (let [{:keys [limit offset] :or {limit 10 offset 0}}
        (html/get-signals req)]
    (if (zero? offset)
      (html/merge-fragment!
       req
       (html/fragment "click-to-load/index.html" {:agents []}))
      (->sse-response
        req
        {on-open
         (fn [sse-gen]
           (d*/with-open-sse sse-gen
             #_(d*/merge-fragment! sse-gen )))}))))
