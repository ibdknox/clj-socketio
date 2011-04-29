(ns clj-socketio.core
  (:import [com.ibdknox.socket_io_netty NSIOServer INSIOHandler INSIOClient SocketIOUtils]))

(def listeners (atom {:connected []
                      :disconnected []
                      :received []
                      :shutdown []}))

(defn route-event [event-type & args]
  (doseq [cur-fn (get @listeners event-type)]
    (apply cur-fn args)))

(def *handler* (reify INSIOHandler 
                 (OnConnect [this client] (route-event :connected client))
                 (OnDisconnect [this client] (route-event :disconnected client))
                 (OnMessage [this client msg] (route-event :received client msg))
                 (OnShutdown [this] (route-event :shutdown))))

(defn- on [event func]
  (swap! listeners #(assoc % event (conj (get % event) func))))

(defn on-connect [func]
  (on :connected func))

(defn on-disconnect [func]
  (on :disconnected func))

(defn on-message [func]
  (on :received func))

(defn on-shutdown [func]
  (on :shutdown func))

(defn send-to [client msg]
  (if-not (coll? client)
    (.send client msg)
    (let [final-msg (SocketIOUtils/encode msg)]
      (doseq [cl client]
        (.sendUnencoded cl final-msg)))))

(defn start [port]
  (NSIOServer/start *handler* port))
