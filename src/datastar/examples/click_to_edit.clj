(ns datastar.examples.click-to-edit
  (:require
   [datastar.html :as html]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def state (atom {}))

(defn edit [req]
  (let [signals (-> req d*/get-signals html/read-json)]
    (swap! state merge signals)
    (->sse-response
     req
     {on-open
      (fn [sse]
        (d*/with-open-sse sse
          (d*/merge-fragment! sse (html/fragment "click-to-edit/index.html" @state))))})))

(defn reset [req]
  (reset! state {})
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (html/fragment "click-to-edit/index.html" @state))))}))

(defn render [req]
  (prn "render full")
  (html/render "click-to-edit.html" @state))

(defn render-edit [req]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (html/fragment "click-to-edit/edit.html" @state))))}))

(defn render-index [req]
  (prn "render-index")
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (html/fragment "click-to-edit/index.html" @state))))}))
