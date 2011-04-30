(ns clj-socketio.core
  (:import [com.ibdknox.socket_io_netty NSIOServer INSIOHandler INSIOClient SocketIOUtils]))

(defonce *servers* (atom {}))

(defn route-event [server event-type & args]
  (doseq [cur-fn (get-in @*servers* [server event-type])]
    (apply cur-fn args)))

(defn- on [server event func]
  (swap! *servers* #(update-in % [server event] conj func)))

(defn on-connect [{server :id} func]
  (on server :connected func))

(defn on-disconnect [{server :id} func]
  (on server :disconnected func))

(defn on-message [{server :id} func]
  (on server :received func))

(defn on-shutdown [{server :id} func]
  (on server :shutdown func))

(defn session-id [client]
  (.getSessionID client))

(defn send-to [client msg]
  (if-not (coll? client)
    (.send client msg)
    (let [final-msg (SocketIOUtils/encode msg)]
      (doseq [cl client]
        (.sendUnencoded cl final-msg)))))

(defn create [port]
  (let [id port
        handler (reify INSIOHandler 
                  (OnConnect [this client] (route-event id :connected client))
                  (OnDisconnect [this client] (route-event id :disconnected client))
                  (OnMessage [this client msg] (route-event id :received client msg))
                  (OnShutdown [this] (route-event id :shutdown)))]
    {:server (NSIOServer. handler port) :port port :id port}))

(defn start [{server :server}]
  (.start server))

(defn stop [{server :server id :id}]
  (.stop server)
  (swap! *servers* #(update-in % [id] (fn [x] nil))))

