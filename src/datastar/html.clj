(ns datastar.html
  (:require
   [cheshire.core :as json]
   [clojure.walk :as walk]
   [ring.util.response :as response]
   [dev.onionpancakes.chassis.core :as h]
   [dev.onionpancakes.chassis.compiler :as hc]
   [starfederation.datastar.clojure.api :as d*]))

(defn response [data]
  (->
    data
    (response/response)
    (response/content-type "text/html")
    (response/charset "UTF-8")))

(defn get-signals [req]
  (-> req
      d*/get-signals
      json/decode
      walk/keywordize-keys))

(defn page [body]
  (h/html
   (hc/compile
    [[h/doctype-html5]
     [:html
      [:head
       [:meta {:charset "UTF-8"}]
       [:title "datastar"]
       [:meta {:name "description", :content ""}]
       [:meta {:name "viewport", :content "width=device-width, initial-scale=1"}]
       [:script {:type "module" :src "https://cdn.jsdelivr.net/gh/starfederation/datastar@v1.0.0-beta.11/bundles/datastar.js"}]
       [:link {:rel "stylesheet" :href "https://matcha.mizu.sh/matcha.css"}]]
      [:body
       [:a {:href "/"} "Home"]
       body]]])))

(defn json [data]
  (json/encode data))
