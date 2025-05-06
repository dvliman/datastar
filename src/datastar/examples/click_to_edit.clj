(ns datastar.examples.click-to-edit
  (:require
   [datastar.html :as html]
   [charred.api :as charred]
   [starfederation.datastar.clojure.api :as d*]
   [starfederation.datastar.clojure.adapter.http-kit :refer [->sse-response on-open]]))

(def ^:private bufSize 1024)
(def read-json (charred/parse-json-fn {:async? false :bufsize bufSize}))


(defn edit [req]
  (let [signals (-> req d*/get-signals read-json)]
    (prn "signals" signals)
    (->sse-response
     req
     {on-open
      (fn [sse]
        (d*/with-open-sse sse
          (d*/merge-fragment! sse (:body (html/render "click-to-edit/edit.html" {})))))})))

(defn reset [req])

(defn render [req]
  (html/render "click-to-edit.html" {}))

(defn get-contact [req]
  (prn "for cancel")
  (->sse-response
   req
   {on-open
    (fn [sse]
      (d*/with-open-sse sse
        (d*/merge-fragment! sse (:body (html/render "click-to-edit/index.html" {})))))}))
