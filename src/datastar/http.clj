(ns datastar.http
  (:require
   [clojure.stacktrace :refer [print-stack-trace]]
   [datastar.html :as html]
   [datastar.examples.click-to-edit :as click-to-edit]
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

(defn catch-exception [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (prn (with-out-str (print-stack-trace e)))
        {:status 500
         :body {:error (.getMessage e)
                :stacktrace (with-out-str (print-stack-trace e))}}))))

(defn home [req]
  (html/render "index.html"))

(defmethod ig/init-key ::handler [_ opts]
  (ring/ring-handler
   (ring/router
    [["/"       {:get {:handler home}}]
     ["/click-to-edit"
      ["/" {:get {:handler click-to-edit/render}}]
      ["/contact/:id"
       ["/"      {:get {:handler click-to-edit/render-index}
                  :put {:handler click-to-edit/edit}}]
       ["/edit"  {:get {:handler click-to-edit/render-edit}}]
       ["/reset" {:put {:handler click-to-edit/reset}}]]]]
    {:conflicts nil
     :exception pretty/exception
     :data {:coercion reitit.coercion.malli/coercion
            :muuntaja muuntaja.core/instance
            :middleware [catch-exception
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         ring-coercion/coerce-exceptions-middleware
                         ring-coercion/coerce-request-middleware
                         ring-coercion/coerce-response-middleware
                         parameters/parameters-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-resource-handler {:path "/"}))))
