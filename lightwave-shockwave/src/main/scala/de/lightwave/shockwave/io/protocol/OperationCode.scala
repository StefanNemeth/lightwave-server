package de.lightwave.shockwave.io.protocol

object OperationCode {
  object Outgoing {
    val Hello: Short = 0
    val Ping: Short = 50
    val SessionParameters: Short = 257
    val CryptoParameters: Short = 277
    val LoginFailed: Short = 33
    val Authenticated: Short = 3
    val PlayerInformation: Short = 5
    val RoomForward: Short = 286
    val FlatInformation: Short = 54
    val RecommendedRoomList: Short = 351
    val LoadingAdvertisementData: Short = 258
    val InitiateRoomLoading: Short = 19
    val FlatLetIn: Short = 41
    val RoomReady: Short = 69
    val Heightmap: Short = 31
    val EntityList: Short = 28
    val PublicObjects: Short = 30
    val FloorItems: Short = 32
    val WallItems: Short = 45
  }

  object Incoming {
    val Pong: Short = 196
    val InitCrypto: Short = 206
    val GenerateKey: Short = 202
    val Login: Short = 4
    val GetPlayerInfo: Short = 7
    val GetFlatInformation: Short = 21
    val Navigate: Short = 150
    val GetRecommendedRooms: Short = 264
    val GetLoadingAdvertisement: Short = 182
    val RoomDirectory: Short = 2
    val TryFlat: Short = 57
    val GoToFlat: Short = 59
    val GetHeightmap: Short = 60
    val GetUsers: Short = 61
    val GetObjects: Short = 62
    val GetItems: Short = 63
  }
}
