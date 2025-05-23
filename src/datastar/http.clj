(ns datastar.http
  (:require
   [datastar.examples.bulk-update :as bulk-update]
   [datastar.examples.click-to-edit :as click-to-edit]
   [datastar.examples.click-to-load :as click-to-load]
   [datastar.examples.delete-row :as delete-row]
   [datastar.html :as html]
   [integrant.core :as ig]
   [muuntaja.core :as m]
   [org.httpkit.server :as httpkit]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as ring-coercion]
   [reitit.ring.middleware.exception :as exception]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]))

(defmethod ig/init-key ::server [_ {:keys [handler] :as opts}]
  (httpkit/run-server handler (dissoc opts :handler)))

(defmethod ig/halt-key! ::server [_ server-stop-fn]
  (server-stop-fn))

(defn home [_]
  (->
   [:ul
    [:li [:a {:href "/click-to-edit/"} "Click to Edit"]]
    [:li [:a {:href "/bulk-update/"} "Bulk Update"]]
    [:li [:a {:href "/click-to-load/"} "Click to Load"]]
    [:li [:a {:href "/delete-row/"} "Delete Row"]]]
   html/page
   html/response))

(defmethod ig/init-key ::handler [_ _]
  (ring/ring-handler
   (ring/router
    [["/"        {:get {:handler home}}]

     ["/click-to-edit"
      ["/"       {:get {:handler click-to-edit/render}}]
      ["/contact/:id"
       ["/"      {:get {:handler click-to-edit/render-index}
                  :put {:handler click-to-edit/edit}}]
       ["/edit"  {:get {:handler click-to-edit/editing}}]
       ["/reset" {:put {:handler click-to-edit/reset}}]]]

     ["/bulk-update"
      ["/"           {:get {:handler bulk-update/render}}]
      ["/activate"   {:put {:handler bulk-update/activate}}]
      ["/deactivate" {:put {:handler bulk-update/deactivate}}]]

     ["/click-to-load"
      ["/"      {:get {:handler click-to-load/render}}]
      ["/more"  {:get {:handler click-to-load/more}}]]

     ["/delete-row"
      ["/"            {:get    {:handler delete-row/render}}]
      ["/delete/:id"  {:delete {:handler delete-row/delete}}]
      ["/reset"       {:get    {:handler delete-row/reset}}]]]
    {:data {:muuntaja m/instance
            :middleware [parameters/parameters-middleware
                         muuntaja/format-negotiate-middleware
                         muuntaja/format-response-middleware
                         exception/exception-middleware
                         muuntaja/format-request-middleware
                         ring-coercion/coerce-exceptions-middleware
                         ring-coercion/coerce-request-middleware
                         ring-coercion/coerce-response-middleware]}})
   (ring/routes
    (ring/redirect-trailing-slash-handler)
    (ring/create-resource-handler {:path "/"}))))
