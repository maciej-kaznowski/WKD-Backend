package com.innercirclesoftware.wkd_core.models

import com.innercirclesoftware.wkd_api.models.Station

/**
 * Station ID is used solely in the WKD requests, we do not want to add these constants to the app API
 */
enum class StationId(val id: Int) {

    GRODZISK_MAZ_RADONSKA(1),
    GRODZISK_MAZ_JORDANOWICE(2),
    GRODZISK_MAZ_PIASKOWA(3),
    GRODZISK_MAZ_OKREZNA(4),
    BRZOZKI(5),
    KAZIMIEROWKA(6),
    MILANOWEK_GRUDOW(27),
    POLESIE(28),
    PODKOWA_LESNA_ZACHODNIA(7),
    PODKOWA_LESNA_GLOWNA(8),
    PODKOWA_LESNA_WSCHODNIA(9),
    OTREBUSY(10),
    KANIE_HELENOWSKIE(11),
    NOWA_WIES_WARSZAWSKA(12),
    KOMOROW(13),
    PRUSZKOW_WKD(14),
    TWORKI(15),
    MALICHY(16),
    REGULY(17),
    MICHALOWICE(18),
    OPACZ(19),
    WARSZAWA_SALOMEA(20),
    WARSZAWA_RAKOW(21),
    WARSZAWA_ALEJE_JEROZOLIMSKIE(22),
    WARSZAWA_REDUTA_ORDONA(23),
    WARSZAWA_ZACHODNIA_WKD(24),
    WARSZAWA_OCHOTA_WKD(25),
    WARSZAWA_SRODMIESCIE_WKD(26);

    companion object {

        private val stationToIdMapping: Map<Station, StationId> = Station.values()
            .associateWith { station -> values().first { stationId -> stationId.name == station.name } }

        fun fromStation(station: Station): StationId {
            return requireNotNull(stationToIdMapping[station]) { "StationId not found for station=$station" }
        }
    }
}

val Station.stationId: StationId
    get() = StationId.fromStation(this)