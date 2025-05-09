(ns datastar.examples.bulk-update
  (:require
   [cheshire.core :as json]
   [datastar.html :as html]))

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

(defn with-key [{:keys [id] :as contact}]
  (merge contact {:key (str "contact_" id)}))

(defn render [_]
  (html/render
   "bulk-update.html"
   {:signals (json/generate-string @state)
    :contacts (map with-key contacts)}))

(defn activate [req]
  (prn (:body-params req))
  (prn (:query-params req))
  (prn (:params req))
  (html/merge-fragment! (html/fragment "bulk-update/contact-row.html" {})))

(defn deactivate [req]
  (html/merge-fragment! (html/fragment "bulk-update/contact-row.html" {})))
