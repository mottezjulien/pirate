
class Server {

  static const String httpAPI = 'http://192.168.1.50:8080';

  static Uri uri(String resourcePath) {
    return Uri.parse('$httpAPI$resourcePath');
  }

}