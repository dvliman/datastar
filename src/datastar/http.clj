(ns datastar.http
  (:require
   [datastar.html :as html]
   [integrant.core :as ig]
   [malli.swagger]
   [muuntaja.core]
   [org.httpkit.server :as httpkit]
   [reitit.coercion.malli]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as ring-coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [selmer.parser]))

(defmethod ig/init-key ::server [_ {:keys [handler] :as opts}]
  (httpkit/run-server handler (dissoc opts :handler)))

(defmethod ig/halt-key! ::server [_ server-stop-fn]
  (server-stop-fn))

(defmethod ig/init-key ::handler [_ opts]
  (ring/ring-handler
   (ring/router
    [["/" {:get {:handler (fn [_] (html/render "index.html"))}}]
     ["/*" {:get {:handler (html/template-handler)}}]]
    {:conflicts nil
     :exception pretty/exception
     :data {:coercion reitit.coercion.malli/coercion
            :muuntaja muuntaja.core/instance
            :middleware [parameters/parameters-middleware
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         ring-coercion/coerce-exceptions-middleware
                         ring-coercion/coerce-request-middleware
                         ring-coercion/coerce-response-middleware]}})
   (ring/routes
    (ring/create-default-handler))))
