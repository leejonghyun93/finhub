{{/*
공통 레이블
*/}}
{{- define "finhub.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
셀렉터 레이블 - 첫 번째 인수로 컴포넌트 이름을 받음
Usage: {{ include "finhub.selectorLabels" (dict "name" "eureka" "Release" .Release) }}
*/}}
{{- define "finhub.selectorLabels" -}}
app: {{ .Release.Name }}-{{ .name }}
{{- end }}

{{/*
공통 Deployment 메타데이터
*/}}
{{- define "finhub.deploymentMeta" -}}
name: {{ .Release.Name }}-{{ .name }}
labels:
  {{- include "finhub.labels" . | nindent 2 }}
  app: {{ .Release.Name }}-{{ .name }}
{{- end }}
