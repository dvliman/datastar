(ns datastar.http
  (:require
   [datastar.html :as html]
   [datastar.examples.click-to-edit :as click-to-edit]
   [integrant.core :as ig]
   [org.httpkit.server :as httpkit]
   [reitit.coercion.malli]
   [reitit.ring :as ring]))

(defmethod ig/init-key ::server [_ {:keys [handler] :as opts}]
  (httpkit/run-server handler (dissoc opts :handler)))

(defmethod ig/halt-key! ::server [_ server-stop-fn]
  (server-stop-fn))

(defn home [_]
  (html/render "index.html"))

(defmethod ig/init-key ::handler [_ _]
  (ring/ring-handler
   (ring/router
    [["/"       {:get {:handler home}}]
     ["/click-to-edit"
      ["/"       {:get {:handler click-to-edit/render}}]
      ["/contact/:id"
       ["/"      {:get {:handler click-to-edit/render-index}
                  :put {:handler click-to-edit/edit}}]
       ["/edit"  {:get {:handler click-to-edit/render-edit}}]
       ["/reset" {:put {:handler click-to-edit/reset}}]]]])
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-resource-handler {:path "/"}))))
