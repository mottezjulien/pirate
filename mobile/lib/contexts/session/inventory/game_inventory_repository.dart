
import '../../../generic/repository/generic_repository.dart';
import '../game_current.dart';
import 'game_inventory.dart';

class GameInventoryRepository {

  String get _basePath => '/sessions/${GameCurrent.sessionId}/inventory';

  Future<List<GameInventorySimple>> list() async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    final response = await genericRepository.get(path: "$_basePath/");
    final List<GameInventorySimple> items = [];
    response.forEach((json) {
      items.add(GameInventorySimple.fromJson(json));
    });
    return items;
  }

  Future<GameInventoryDetail> details(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    final response = await genericRepository.get(path: "$_basePath/$itemId");
    return GameInventoryDetail.fromJson(response);
  }

  Future<void> drop(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.delete(path: "$_basePath/$itemId");
  }

  Future<void> use(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.post(path: "$_basePath/$itemId/use", decode: false);
  }

  Future<void> consume(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.post(path: "$_basePath/$itemId/consume", decode: false);
  }

  Future<void> equip(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.post(path: "$_basePath/$itemId/equip", decode: false);
  }

  Future<void> unequip(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.post(path: "$_basePath/$itemId/unequip", decode: false);
  }

  Future<void> useEquip(String itemId) async {
    GenericGameSessionRepository genericRepository = GenericGameSessionRepository();
    await genericRepository.post(path: "$_basePath/$itemId/equip/use", decode: false);
  }
}



