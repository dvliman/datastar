(ns datastar.html
  (:require
   [cheshire.core :as json]
   [clojure.walk :as walk]
   [ring.util.response :as response]
   [selmer.parser :as selmer]
   [dev.onionpancakes.chassis.core :as h]
   [dev.onionpancakes.chassis.compiler :as hc]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(defn response [data]
  (->
    data
    (response/response)
    (response/content-type "text/html")
    (response/charset "UTF-8")))

(defn fragment
  ([template-path]
   (fragment template-path {}))
  ([template-path data]
   (->
    (str "templates/" template-path)
    (selmer/render-file data))))

(defn render
  ([template-path]
   (render template-path {}))
  ([template-path data]
   (response (fragment template-path data))))

(defmacro merge-fragment! [req & body]
  `(->sse-response
    ~req
    {on-open
     (fn [sse-gen#]
       (d*/with-open-sse sse-gen#
         (d*/merge-fragment! sse-gen# ~@body)))}))

(defmacro merge-fragments! [req & body]
  `(->sse-response
      ~req
    {on-open
     (fn [sse-gen#]
       (d*/with-open-sse sse-gen#
         (d*/merge-fragments! sse-gen# ~@body)))}))

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
