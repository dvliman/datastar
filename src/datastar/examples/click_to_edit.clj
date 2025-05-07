(ns datastar.examples.click-to-edit
  (:require
   [cheshire.core :as json]
   [datastar.html :as html]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def state (atom {:firstName "John"
                  :lastName "Doe"
                  :email "joe@blow.com"}))

(defn edit [req]
  (let [signals (-> req :body-params)]
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
  (html/render "click-to-edit.html" @state))

(defn render-edit [req]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (html/fragment "click-to-edit/edit.html" {}
                                               #_{:signals (cheshire.core/generate-string @state)}))))}))

(defn render-index [req]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (html/fragment "click-to-edit/index.html" @state))))}))
