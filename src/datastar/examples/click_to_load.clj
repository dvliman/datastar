(ns datastar.examples.click-to-load
  (:require
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]
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

(defn agent-row [i]
  [:tr {:id (str "agent_" i)}
   [:td "Agent Smith"]
   [:td (format "void%d@null.org" (inc i))]
   [:td (alias-hash (format "%s" (inc 1)))]])

(def state
  (atom {:offset 0 :limit 10}))

(defn render [_]
  (->
   [:div#click-to-load {:data-signals (html/json @state)}
    [:table
     [:thead
      [:tr
       [:th "Name"]
       [:th "Email"]
       [:th "ID"]]]
     [:tbody#click-to-load-rows {:data-signals (html/json @state)}
      (for [i (range (:limit @state))]
        (agent-row i))]]
    [:button {:id "more"
              :data-on-click
              (h/raw (format "$offset=%d;$limit=%d;@get('/click-to-load/more')"
                             (+ (:offset @state)
                                (:limit @state)) (:limit @state)))}
     "Load More"]]
   html/page
   html/response))

(defn cap [limit]
  (if (> limit 100)
    100
    limit))

(defn more [req]
  (let [{:keys [limit offset] :or {limit 10 offset 0}} (html/get-signals req)
        limit (cap limit)]
    (swap! state merge {:limit limit :offset offset})
    (->sse-response
     req
     {on-open
      (fn [sse]
        (d*/merge-fragments!
         sse
         (for [i (map (partial + (:offset @state)) (range (:limit @state)))]
           (h/html (agent-row i)))
         {d*/selector "click-to-load-rows"
          d*/merge-mode d*/mm-append}))})))
