(ns datastar.examples.click-to-edit
  (:require
   [datastar.html :as html]))

(def state (atom {:firstName "John"
                  :lastName "Doe"
                  :email "joe@blow.com"}))

(defn render [req]
  (html/render "click-to-edit.html" @state))

(defn render-edit [req]
  (html/merge-fragment! req (html/fragment "click-to-edit/edit.html" {})))

(defn render-index [req]
  (html/merge-fragment! req (html/fragment "click-to-edit/index.html" @state)))

(defn edit [req]
  (let [signals (-> req :body-params)]
    (html/merge-fragment! req (html/fragment "click-to-edit/index.html" (swap! state merge signals)))))

(defn reset [req]
  (html/merge-fragment! req (html/fragment "click-to-edit/index.html" (reset! state {}))))
