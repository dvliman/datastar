(ns datastar.examples.bulk-update
  (:require
   [clojure.string :as str]
   [datastar.html :as html]
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

(defn with-key [{:keys [id] :as contact}]
  (assoc contact :key (str "contact_" id)))

(defn render [_]
  (html/render
   "bulk-update.html"
   {:signals @state
    :contacts (map with-key contacts)}))

(defn contact? [c]
  (str/starts-with? (name c) "contact_"))

(defn contact-id [s]
  (parse-long (second (re-find #"contact_(\d+)" (name s)))))

(defn activation [selections is-active]
  (map with-key
       (reduce (fn [acc [select selected?]]
                 (if (contact? select)
                   (let [contact (nth acc (contact-id select))
                         changed? (not (= (:is-active contact) is-active))]
                     (if (and selected? changed?)
                       (update-in acc [(contact-id select) :is-active] (constantly is-active))
                       acc))
                   acc))
               contacts
               selections)))

(defn activate [{:keys [body-params] :as req}]
  (html/merge-fragment!
   req
   (for [contact (activation body-params true)]
     (html/fragment "bulk-update/contact-row.html" {:contact (with-key contact)}))))

(defn deactivate [{:keys [body-params] :as req}]
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse))}))
