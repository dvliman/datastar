(ns datastar.examples.bulk-update
  (:require
   [clojure.string :as str]
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
  (for [[select selected?] selections]
    (when (contact? select)
      (let [contact (nth contacts (contact-id select))
            changed? (not (= (:is-active contact) is-active))]
        (when (and selected? changed?)
          (update-in contacts [(contact-id select) :is-active] (constantly is-active)))))))

(identity contacts)
(activation {:contact_2 false
             :contact_3 true} true)
(defn activate [req]
  (activation req true))

(defn deactivate [req]
  (activation req false))

(macroexpand
 '(html/merge-fragment!
   req
   (html/fragment
    "bulk-update/contact-row.html"
    {:contact (update contact :is-active (constantly is-active))})
   (html/fragment
    "bulk-update/contact-row.html"
    {:contact (update contact :is-active (constantly is-active))})))
