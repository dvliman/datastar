(ns datastar.examples.delete-row
  (:require
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]
   [dev.onionpancakes.chassis.compiler :as hc]))

(defn delete-row-contact [contact]
  (h/html
   (hc/compile
    [:div.delete-row])))

(defn delete-row-contacts [contacts]
  (html/response
   (h/html
    (html/page
     (hc/compile
      [:div.delete-row
       [:table
        [:thead
         [:tr
          [:td "Name"]
          [:td "Email"]
          [:td "Status"]
          [:td "Actions"]]]
        [:tbody
         #_(for [contact contacts]
           (delete-row-contact contact))]]
       [:div
        [:button {:data-on-click "@get('delete-row/reset')"}
         "Reset"]]])))))
