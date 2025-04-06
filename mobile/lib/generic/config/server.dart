
class Server {

  static const String httpAPI = 'http://localhost:8080';

  static Uri uri(String resourcePath) {
    return Uri.parse('$httpAPI$resourcePath');
  }

}