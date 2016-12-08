<template>
  <footer class="footer">
    <div class="container">
      <div class="content has-text-centered">
          <p>
            <strong>Kakapo prototyped</strong> by Zlata Bruce
          </p>
          <p>
            Copyright © 2016 Zlata Bruce
          </p>
    </div>
    </div>
  </footer>
</template>

<script>
export default {

  data () {
    return this.$store.state.pkg
  }

}
</script>

<style lang="scss">
.footer {
  .social a {
    border-bottom: none !important;
  }

  .fa.fa-heart {
    color: red;
  }
}
</style>
