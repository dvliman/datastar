(ns datastar.examples.delete-row
  (:require
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]))

(def contacts
  [{:id 0 :name "Joe Smith"       :email "joe@smith.org"       :is-active true}
   {:id 1 :name "Angie MacDowell" :email "angie@macdowell.org" :is-active true}
   {:id 2 :name "Fuqua Tarkenton" :email "fuqua@tarkenton.org" :is-active true}
   {:id 3 :name "Kim Yee"         :email "kim@yee.org"         :is-active false}])

(defn delete-row-contact [contact]
  [:tr {:id (str "contact_" (:id contact))}
   [:td (:name contact)]
   [:td (:email contact)]
   [:td (if (:is-active contact) "Active" "Inactive")]
   [:td
    [:button {:data-on-click (h/raw (format "confirm('are you sure?') && @delete('delete-row/data/%s')" (:id contact)))}
     "Delete"]]])

(defn delete-row-contacts [_req]
  (->
   [:div.delete-row
    [:table
     [:thead
      [:tr
       [:td "Name"]
       [:td "Email"]
       [:td "Status"]
       [:td "Actions"]]]
     [:tbody
      (for [contact contacts]
        (delete-row-contact contact))]]
    [:div
     [:button {:data-on-click (h/raw "@get('/delete-row/reset')")}
      "Reset"]]]
   html/page
   html/response))
