package com.example

import android.app.Application
import com.example.ble.BleHeartRateManager
import com.example.ble.SimulatorManager
import com.example.data.AppDatabase
import com.example.data.SessionRepository
import com.example.osc.OscSender
import com.example.server.OverlayHttpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PulsoidApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var sessionRepository: SessionRepository
        private set
    lateinit var bleManager: BleHeartRateManager
        private set
    lateinit var simulatorManager: SimulatorManager
        private set
    lateinit var oscSender: OscSender
        private set
    lateinit var overlayServer: OverlayHttpServer
        private set

    companion object {
        lateinit var instance: PulsoidApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        sessionRepository = SessionRepository(database.sessionDao())
        bleManager = BleHeartRateManager(this)
        simulatorManager = SimulatorManager()
        oscSender = OscSender()
        overlayServer = OverlayHttpServer(this, 9090)

        // Seed demo sessions on first launch so user immediately sees rich session history
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sessionRepository.seedSampleSessionsIfEmpty()
            } catch (_: Exception) {}
        }
    }
}
