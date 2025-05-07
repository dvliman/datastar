(ns datastar.examples.click-to-edit
  (:require
   [datastar.html :as html]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def state (atom {}))

(defn edit [req]
  (let [signals (-> req d*/get-signals html/read-json)]
    #d signals
    (swap! state merge signals)
    #d @state
    (->sse-response
     req
     {on-open
      (fn [sse]
        (d*/with-open-sse sse
          (d*/merge-fragment! sse (:body (html/render "click-to-edit.html" @state)))))})))

(defn reset [req]
  (reset! state {})
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (:body (html/render "click-to-edit.html" @state)))))}))

(defn render [req]
  (html/render "click-to-edit.html" @state))

(defn render-edit [req]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (:body (html/render "click-to-edit/edit.html" @state)))))}))
