(ns datastar.examples.click-to-edit
  (:require
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def state
  (atom
   {:firstName "John"
    :lastName "Doe"
    :email "joe@blow.com"}))

(defn view []
  (let [{:keys [firstName lastName email]} @state]
    [:div#contact_1
     [:label "First Name: " firstName]
     [:label "Last Name: " lastName]
     [:label "Email: " email]
     [:div
      [:button {:data-on-click (h/raw "@get('/click-to-edit/contact/1/edit')")} "Edit"]
      [:button {:data-on-click (h/raw "@put('/click-to-edit/contact/1/reset')")} "Reset"]]]))

(defn render [_]
  (html/response (html/page (view))))

(defn fragment [req]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/merge-fragment! sse (h/html (view))))}))

(defn editing [req]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/merge-fragment!
       sse
       (h/html
        [:div {:id "contact_1" :data-signals (html/json @state)}
         [:label "First Name" [:input {:type "text" :data-bind "firstName"}]]
         [:label "Last Name" [:input {:type "text" :data-bind "lastName"}]]
         [:label "Email" [:input {:type "text" :data-bind "email"}]]
         [:div
          [:button {:data-on-click (h/raw "@put('/click-to-edit/contact/1/')")} "Save"]
          [:button {:data-on-click (h/raw "@get('/click-to-edit/contact/1/')")} "Cancel"]]])))}))

(defn edit [req]
  (let [signals (-> req :body-params)]
    (swap! state merge signals)
    (fragment req)))

(defn reset [req]
  (reset! state {})
  (fragment req))
