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

(defn make-agent [i]
  {:id (str "agent_" i)
   :email (format "void%d@null.org" (inc i))
   :alias-hash (alias-hash (format "%s" (inc 1)))})

(defn render [req]
  (html/render
   "click-to-load.html"
   {:signals {:limit 10 :offset 0}
    :limit-plus-offset (+ 10 0)}))

(defn cap [limit]
  (if (> limit 100)
    100
    limit))

(defn more [req]
  (let [{:keys [limit offset] :or {limit 10 offset 0}} (html/get-signals req)
        limit (cap limit)]
    (html/merge-fragment!
     req
     (html/fragment
      "click-to-load/agent-row.html"
      {:signals {:limit limit :offset (+ limit offset)}
       :agents (map make-agent (range offset limit))}))))
