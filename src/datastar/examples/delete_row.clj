(ns datastar.examples.delete-row
  (:require
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def contacts
  [{:id 0 :name "Joe Smith"       :email "joe@smith.org"       :is-active true}
   {:id 1 :name "Angie MacDowell" :email "angie@macdowell.org" :is-active true}
   {:id 2 :name "Fuqua Tarkenton" :email "fuqua@tarkenton.org" :is-active true}
   {:id 3 :name "Kim Yee"         :email "kim@yee.org"         :is-active false}])

(def state (atom contacts))

(defn delete-row [contact]
  [:tr {:id (str "contact_" (:id contact))}
   [:td (:name contact)]
   [:td (:email contact)]
   [:td (if (:is-active contact) "Active" "Inactive")]
   [:td
    [:button {:data-on-click (h/raw (format "confirm('are you sure?') && @delete('/delete-row/delete/%s')" (:id contact)))}
     "Delete"]]])

(defn render [_]
  (->
   [:div#delete-row
    [:table
     [:thead
      [:tr
       [:td "Name"]
       [:td "Email"]
       [:td "Status"]
       [:td "Actions"]]]
     [:tbody
      (for [contact @state]
        (delete-row contact))]]
    [:div
     [:button {:data-on-click "@get('/delete-row/reset')"}
      "Reset"]]]
   html/page
   html/response))

(defn delete [req]
  (let [id (-> req :path-params :id parse-long)]
    (swap! state (partial filter (fn [x]
                                   (not= id (:id x)))))
    (->sse-response
     req
     {on-open
      (fn [sse]
        (d*/remove-fragment! sse (str "contact_" id))
        (d*/redirect! sse "/delete-row"))})))

(defn reset [req]
  (reset! state contacts)
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/redirect! sse "/delete-row"))}))
