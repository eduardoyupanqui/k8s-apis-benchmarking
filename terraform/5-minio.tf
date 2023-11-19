data "kubectl_path_documents" "minio" {
  pattern = "../minio/*.yaml"
}

resource "kubectl_manifest" "minio" {
  for_each  = toset(data.kubectl_path_documents.minio.documents)
  yaml_body = each.value

  depends_on = [helm_release.tempo]
}