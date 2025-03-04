<template>
  <div class="dropdown">
    <div data-bs-toggle="dropdown" ref="activationwrapper">
      <slot name="activation"></slot>
    </div>
    <ul class="dropdown-menu" role="menu" :id="menuid">
      <li v-for="item in items" :key="item.id" class="divider" @mouseenter="showSubMenu($event)">
        <a
          v-if="item.show"
          class="dropdown-item"
          :role="getAnchorRole(item)"
          :tabindex="!item.url ? '0' : undefined"
          :href="item.url"
          @click="handleClick(item, $event)"
          @keyup.enter="handleClick(item, $event)"
          @keyup.space="item.action ? handleClick(item, $event) : undefined"
        >
          <sakai-icon
            :iconkey="item.icon"
            class="icon-wrap"
            :class="item.icon"
          />
          {{ item.string }}
        </a>
        <ul v-if="item.subMenu" role="menu" :class="['dropdown-submenu', isSubMenuOverflowing ? 'left' : 'right']">
          <li v-for="subItem in item.subMenu" :key="subItem.string" class="divider">
            <a
              v-if="subItem.show"
              class="dropdown-item"
              @click="handleClick(subItem, $event)"
            >
             <sakai-icon :iconkey="subItem.icon" class="icon-wrap" :class="item.icon"/>
               {{ subItem.string }}
            </a>
          </li>
      </ul>
      </li>
    </ul>
  </div>
</template>

<script>
//import "/node_modules/bootstrap/js/src/dropdown.js";
import SakaiIcon from "./sakai-icon.vue";
export default {
  components: {
    SakaiIcon,
  },
  props: {
    items: {
      type: Array,
      default: () => [
        {
          id: 0,
          icon: "error",
          string: "emty",
          route: "/",
        },
      ],
    },
    menuid: { type: String, default: null },
  },
  data() {
    return {
      selectedId: null,
      expanded: false,
      isSubMenuOverflowing: false,
    };
  },
  computed: {},
  methods: {
    getAnchorRole(item) {
      if (item.action) {
        return "button";
      } else {
        return "link";
      }
    },
    handleClick(item, event) {
      this.selectedId = item.id;
      if (event) {
        event.preventDefault();
      }
      if (item.route) {
        this.handleRoute(item.route);
      } else if (item.action) {
        item.action();
      }
    },
    showSubMenu(event) {
        const submenu = event.currentTarget.querySelector('.dropdown-submenu');

        if (submenu) {
          const rect = submenu.getBoundingClientRect();
          const rightEdge = rect.right;

          if (rightEdge > window.innerWidth) {
            this.isSubMenuOverflowing = true;
          }
        }
    },
    handleRoute(route) {
      this.$router.push({ path: route });
    },
    onMutation(mutationsList) {
      for (const mutation of mutationsList) {
        if (
          mutation.type === "attributes" &&
          mutation.attributeName === "aria-expanded"
        ) {
          this.expanded =
            mutation.target.attributes["aria-expanded"].value === "true";
        }
      }
    },
  },
  mounted() {
    const observer = new MutationObserver(this.onMutation);
    observer.observe(this.$refs.activationwrapper, { attributes: true });
  },
};
</script>

<style scoped lang="scss">
#meetings-tool {
.icon-wrap {
  width: 16px;
  margin-right: 6px;
}
.dropdown-menu {
  background-color: var(--tool-menu-background-color);
  box-shadow: var(--elevation-1dp);
  border: 1px solid var(--button-border-color);
}
.dropdown-item,
.dropdown-item:hover,
.dropdown-item:focus {
  padding: 0.35rem 1rem;
  color: var(--tool-menu-item-text-color);
}
.dropdown-item:hover,
.dropdown-item:focus {
  background-color: var(--tool-menu-item-hover-background-color);
  cursor: pointer;
}
.dropdown-item:focus {
  outline: 3px solid var(--focus-outline-color);
}
@media (max-width: 600px) {
  .dropdown-menu {
    inset: auto auto -8px 0px !important;
    transform: unset !important;
    position: fixed !important;
    padding: 0.6rem 2.5rem 0.8rem 2.5rem;
    box-shadow: 0 0 0 100vmax #9f9f9f42;
    border-radius: 10px;
    width: 100%;
  }
  .dropdown-item {
    padding: 1rem 1rem 1rem 30% !important;
  }
  .divider {
    border-bottom: 1px solid var(--sakai-border-color);
  }
  .divider:last-child {
    border-bottom: none;
  }
}

.dropdown-submenu {
  box-shadow: var(--elevation-1dp);
  border: 1px solid var(--button-border-color);
  background-color: var(--tool-menu-background-color);
  position: absolute;
  padding: 0;
  left: 100%;
  top: 100px;
  display: none;
  border-radius: 10px;
  list-style: none;
}

li:hover > .dropdown-submenu {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}
.dropdown-submenu.left {
  left: auto;
  right: 100%;
}

.dropdown-submenu.right {
  left: 100%;
}
}
</style>
