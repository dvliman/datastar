(ns datastar.examples.edit-row
  (:require
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def state (atom {}))

(defn edit-row-contact [contacts i is-editing-row is-editing-any-row]

(defn render []
  (html/response
   (html/page
    [:div {:id "edit-row" :data-signals (html/json @state)}
     [:table
      [:caption "Contacts"]
      [:thead
       [:tr
        [:th "Name"]
        [:th "Email"]
        [:th "Actions"]]]
      [:tbody {:id "edit-row-table-body"}
       (for [[i _] (map-indexed vector (:contacts @state))]
         (edit-row-contact
          (:contacts @state)
          i
          (= i (:edit-row-index @state))
          (not= -1 (:edit-row-index @state))))]
      [:div
       [:button
        {:data-testid "reset"
         :data-on-click (h/raw "@get('/edit-row/reset')")}
        "Reset"]]]])))
