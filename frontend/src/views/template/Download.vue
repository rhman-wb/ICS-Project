<template>
  <div class="template-download">
    <h1>下载模板</h1>
    <p>这是模板下载页面</p>
    <div class="download-section">
      <button @click="downloadTemplate" class="download-btn">
        <i class="fa fa-download"></i>
        下载 Excel 模板
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
const downloadTemplate = () => {
  // 模拟下载模板文件
  const link = document.createElement('a')
  link.href = '/template.xlsx'
  link.download = '产品导入模板.xlsx'
  link.click()
}
</script>

<style scoped lang="scss">
.template-download {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  color: #001529;
  margin-bottom: 20px;
}

.download-section {
  margin-top: 30px;
  text-align: center;
}

.download-btn {
  background-color: #0079FE;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #0056b3;
  }

  i {
    font-size: 18px;
  }
}
</style>