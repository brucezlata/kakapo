<template>
  <section class="hero is-bold app-navbar animated" :class="{ slideInDown: show, slideOutDown: !show }">
    <div class="hero-head">
      <nav class="nav">

        <!-- <div class="nav-left">
          <a class="nav-item is-hidden-tablet" @click="$store.dispatch('TOGGLE_SIDEBAR')">
            <i class="fa fa-bars" aria-hidden="true"></i>
          </a>
        </div> -->

        <div class="nav-left">
          <a class="nav-item is-brand" href="#">
            <img src="~assets/top_logo.png" alt="logo">
          </a>
        </div>


        <div class="nav-center">
          <a class="nav-item hero-brand" href="/">
            <img src="~assets/kakapo.png" :alt="description">
            <tooltip :label="'v' + version" placement="right" type="success" size="small" :no-animate="true" :always="true" :rounded="true">
              <div class="is-hidden-mobile">
                <strong class="admin">Kakapo:Cold Chain Track&Trace Platform</strong>
              </div>
            </tooltip>
          </a>
        </div>

        <div class="nav-right is-flex">
          <!-- <strong class="admin">{{ description }}</strong> -->
        </div>
      </nav>
    </div>
  </section>
</template>

<script>
import Tooltip from 'vue-bulma-tooltip'

export default {

  components: {
    Tooltip
  },

  props: {
    show: Boolean
  },

  data () {
    return this.$store.state.pkg
  }

}
</script>

<style lang="scss">
@import '~bulma/sass/utilities/variables';

.app-navbar {
  position: fixed;
  min-width: 100%;
  z-index: 1024;
  box-shadow: 0 2px 3px rgba(17, 17, 17, 0.1), 0 0 0 1px rgba(17, 17, 17, 0.1);

  .container {
    margin: auto 10px;
  }

  .nav-right {
    align-items: stretch;
    align-items: stretch;
    flex: 1;
    justify-content: flex-end;
    overflow: hidden;
    overflow-x: auto;
    white-space: nowrap;
  }
}

.hero-brand {
  .vue {
    margin-left: 10px;
    color: #36AC70;
  }
  .admin {
    color: #28374B;
  }
}
</style>
