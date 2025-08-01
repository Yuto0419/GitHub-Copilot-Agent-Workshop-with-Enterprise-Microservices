# Microservices Port Configuration Summary

以下のポート設定で全サービスが統一されました：

## サービス別ポート設定

| サービス名 | ポート番号 | 修正内容 |
|-----------|-----------|----------|
| **Authentication Service** | 8080 | ✅ 正しく設定済み |
| **User Management Service** | 8081 | ✅ application.ymlにポート設定追加 |
| **Inventory Management Service** | 8082 | ✅ 正しく設定済み + Dockerfile/docker-compose.yml追加 |
| **Sales Management Service** | 8083 | ✅ 正しく設定済み |
| **Payment Cart Service** | 8084 | ✅ 正しく設定済み |
| **Point Service** | 8085 | ✅ 正しく設定済み + Dockerfile/docker-compose.yml追加 |
| **Coupon Service** | 8086 | ✅ 8088 → 8086に修正（application.yml + Dockerfile） |
| **AI Support Service** | 8087 | ✅ 8089 → 8087に修正 + Dockerfile/docker-compose.yml追加 |
| **API Gateway** | 8090 | ✅ application.ymlにポート設定追加 + docker-compose.yml追加 |
| **Frontend Service** | 3000 | ✅ 8080 → 3000に修正（application.yml + Dockerfile） |

## 修正されたファイル

### Application Properties
- ✅ `user-management-service/src/main/resources/application.yml` - ポート8081追加
- ✅ `coupon-service/src/main/resources/application.yml` - ポート8088→8086
- ✅ `ai-support-service/src/main/resources/application.yml` - ポート8089→8087
- ✅ `api-gateway/src/main/resources/application.yml` - ポート8090追加
- ✅ `frontend-service/src/main/resources/application.yml` - ポート8080→3000

### Dockerfile
- ✅ `frontend-service/Dockerfile` - EXPOSE 8080→3000, healthcheck URL修正
- ✅ `coupon-service/Dockerfile` - EXPOSE 8088→8086, healthcheck URL修正
- ✅ `ai-support-service/Dockerfile` - 新規作成（EXPOSE 8087）
- ✅ `inventory-management-service/Dockerfile` - 新規作成（EXPOSE 8082）
- ✅ `point-service/Dockerfile` - 新規作成（EXPOSE 8085）

### Docker Compose Files
- ✅ 全サービスで`network_mode: host`使用時の`ports`設定を削除
- ✅ healthcheck URLのポート番号を正しいポートに修正
- ✅ `ai-support-service/docker-compose.yml` - 新規作成
- ✅ `inventory-management-service/docker-compose.yml` - 新規作成
- ✅ `point-service/docker-compose.yml` - 新規作成
- ✅ `api-gateway/docker-compose.yml` - 新規作成

### README Files
- ✅ `frontend-service/README.md` - 全てのURL参照を3000番ポートに修正
- ✅ `api-gateway/README.md` - 正しいポート設定を確認済み

### Service URLs in Configuration
- ✅ `frontend-service/application.yml` - API Gateway URL を8090に修正
- ✅ `frontend-service/application.yml` - AI Support Service URL を8087に修正

## 重要な注意点

1. **Docker Compose使用時**: `network_mode: host`により、サービスはホストネットワーク上で直接実行されます
2. **Dev Container環境**: VS Codeが自動的にポートフォワーディングを提供します
3. **ポート競合回避**: Frontend Serviceを3000番ポートに変更してAPI Gateway(8090)との競合を回避
4. **Dockerfile整合性**: 全サービスでEXPOSEポートとhealthcheck URLが一致

## サービス起動後のアクセスURL

| サービス | アクセスURL |
|---------|------------|
| Authentication Service | http://localhost:8080 |
| User Management Service | http://localhost:8081 |
| Inventory Management Service | http://localhost:8082 |
| Sales Management Service | http://localhost:8083 |
| Payment Cart Service | http://localhost:8084 |
| Point Service | http://localhost:8085 |
| Coupon Service | http://localhost:8086 |
| AI Support Service | http://localhost:8087 |
| API Gateway | http://localhost:8090 |
| Frontend Service | http://localhost:3000 |

## 新規作成されたファイル

- `ai-support-service/Dockerfile`
- `ai-support-service/docker-compose.yml`
- `inventory-management-service/Dockerfile`
- `inventory-management-service/docker-compose.yml`
- `point-service/Dockerfile`
- `point-service/docker-compose.yml`
- `api-gateway/docker-compose.yml`

これで全てのマイクロサービスが統一されたポート設定で動作し、Dockerfile、docker-compose.yml、application.ymlの設定が完全に整合しました。
