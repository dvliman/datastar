(ns datastar.examples.bulk-update
  (:require
   [clojure.string :as str]
   [datastar.html :as html]
   [dev.onionpancakes.chassis.core :as h]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def contacts
  [{:id 0 :name "Joe Smith"       :email "joe@smith.org"       :is-active true}
   {:id 1 :name "Angie MacDowell" :email "angie@macdowell.org" :is-active true}
   {:id 2 :name "Fuqua Tarkenton" :email "fuqua@tarkenton.org" :is-active true}
   {:id 3 :name "Kim Yee"         :email "kim@yee.org"         :is-active false}])

(def state
  (atom
   {:selections
    (->> contacts
         (map (comp (partial str "contact_") :id))
         (cons "all")
         (map (fn [k] {k false}))
         (into {}))}))

(defn contact-row [contact]
  [:tr {:id (str "contact_" (:id contact))}
   [:td [:input {:class "checkbox" :type "checkbox" :data-bind (str "selections.contact_" (:id contact))}]]
   [:td (:name contact)]
   [:td (:email contact)]
   [:td (if (:is-active contact) "Active" "Inactive")]])

(defn render [_]
  (->
   [:div#bulk-update {:data-signals (html/json @state)}
    [:table
     [:caption "Select row and activate or deactivate below"]
     [:thead
      [:tr
       [:th
        [:input
         {:class "checkbox"
          :type "checkbox"
          :data-bind "selections.all"
          :data-on-change "@setAll('selections.contact_*', $selections.all)"}]]
       [:th "Name"]
       [:th "Email"]
       [:th "Status"]]]
     [:tbody
      (for [contact contacts]
        (contact-row contact))]]
    [:div
     [:button {:data-on-click (h/raw "@put('/bulk-update/activate'); $selections.all = false; @setAll('selections.contact_', $selections.all)")} "Activate"]
     [:button {:data-on-click (h/raw "@put('/bulk-update/deactivate'); $selections.all = false; @setAll('selections.contact_', $selections.all)")} "Deactivate"]]]
   html/page
   html/response))

(defn contact? [c]
  (str/starts-with? (name c) "contact_"))

(defn contact-id [s]
  (parse-long (second (re-find #"contact_(\d+)" (name s)))))

(defn activation [selections is-active]
  (reduce (fn [acc [select selected?]]
            (if (contact? select)
              (let [contact (nth acc (contact-id select))
                    changed? (not (= (:is-active contact) is-active))]
                (if (and selected? changed?)
                  (update-in acc [(contact-id select) :is-active] (constantly is-active))
                  acc))
              acc))
          contacts
          selections))

(defn activate [{:keys [body-params] :as req}]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/merge-fragments!
       sse
       (map (comp h/html contact-row) (activation (:selections body-params) true))))}))

(defn deactivate [{:keys [body-params] :as req}]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/merge-fragments!
       sse
       (map (comp h/html contact-row) (activation (:selections body-params) false))))}))
