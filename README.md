# MiniGamePlayground — Simply Skills Compatibility

可选附属模组，用于在 MiniGamePlayground 区域内整体禁用 Simply Skills 的技能、效果、属性奖励和死亡防护。

核心模组本身不依赖 Simply Skills。只有同时安装：

- `minigameplayground` 2.5.0+
- `simplyskills` 1.7.2+ / 1.20.1
- `puffish_skills` 0.15.0+（Simply Skills 的前置）
- 本附属模组

才会启用区域内的兼容控制。

## 解决什么问题

Simply Skills 的 `Undying` 效果会在玩家生命降到 0 时把生命设置为 1，并阻止死亡。测试中表现为：

```text
致命伤害 → 生命变成 1
再次致命伤害 → 生命仍然是 1
```

这是 Simply Skills 的设计行为，不是 MiniGamePlayground 的伤害公式。其他可能造成“打不死”或“后续伤害被取消”的机制包括：

| Simply Skills 机制 | 效果 |
| --- | --- |
| `Undying` | 生命降到 0 时设为 1，阻止死亡 |
| `Death Defy` / Armor Mastery | 低生命时获得抗性提升 |
| `Barrier` | 取消一次伤害 |
| `Ghostwalk` | 持续期间取消伤害 |
| `Torment` | 取消/转移伤害 |
| `Evasion Mastery` | 概率完全闪避攻击 |
| `Bone Armor` | 提供额外吸收 |
| Dynamic Damage Reduction (DDR) | 连续命中时降低后续伤害 |

## 区域选项

在核心模组中设置：

```text
/mgp simplyskills <区域ID>
/mgp simplyskills <区域ID> disabled
/mgp simplyskills <区域ID> enabled
```

`disabled` 表示在区域内整体禁用 Simply Skills；`enabled` 表示保持 Simply Skills 原行为。默认 `enabled`。

`/mgp list <区域>` 会显示：

```text
SimplySkills: 禁用 / 启用
```

## 禁用范围

区域内会：

1. 移除玩家身上的 `simplyskills:*` 状态效果；
2. 阻止新的 `simplyskills:*` 状态效果被添加；
3. 在受击前再次清理 Simply Skills 效果；
4. 让 `HelperMethods.isUnlocked` 在区域内返回 `false`，关闭所有依赖技能树的被动和主动技能；
5. 关闭 Simply Skills 的 `Undying` 死亡阻止；
6. 关闭 `Dynamic Damage Reduction`；
7. 关闭 Rogue `Evasion Mastery` 的完全闪避；
8. 关闭 Ascendancy `Torment` 的伤害取消；
9. 移除 Puffish Skills 的属性奖励（例如技能树提供的攻击、生命、速度等），离开区域后重新计算并恢复。

区域外不改变 Simply Skills 的任何行为。

## 没有禁用的内容

本附属不会：

- 删除玩家的 Puffish Skills 技能树、等级或已花费点数；
- 禁用其他模组提供的法术、武器或被动；
- 清理 Simply Skills 直接写入的普通属性（如果有）；
- 阻止 Simply Skills 的客户端 HUD/动画。

如果测试仍发现某个技能没有被禁用，请提供技能名称或效果 ID，以便补充对应的兼容钩子。

## 伤害兜底已迁移到本附属

核心模组原本有一个专门为 Simply Skills 准备的 `actuallyHurt` 最终生命值兜底。现在该兜底已从核心移除并迁移到本附属：

- 使用核心的 `DamageFallbackContext` 判断正常伤害倍率是否已经执行；
- 如果 Simply Skills 取消了核心的 `getDamageAfterMagicAbsorb` 回调，附属会按最终生命损失重新应用一次伤害平衡；
- 兜底覆盖所有游戏区域，恢复从核心迁移前的行为；
- 不再直接调用 `die()`，因此不会绕过不死图腾或其他死亡保护。

进入区域后在玩家 tick 开始时同步移除效果和属性奖励，不等到服务器 tick 结束。

## 与其他模组的关系

- Simply Skills 和核心模组都会在 `getDamageAfterMagicAbsorb` 的 RETURN 阶段处理伤害。
- 本附属关闭 DDR、技能树和防护效果后，核心伤害倍率在区域内可以稳定应用。
- 因为 Puffish Skills 没有在奖励更新上下文中提供所属分类，本附属会暂时移除**所有** Puffish 属性奖励；如果服务器同时使用其他 Puffish 技能树，它们在该区域内也会被暂停。

## 构建

先构建核心模组：

```bash
cd ../minigameplayground-core
./gradlew build
```

再构建本附属：

```bash
cd ../minigameplayground-simplyskills
./gradlew build
```

产物：

```text
build/libs/minigameplayground-simplyskills-1.1.0.jar
```

构建时需要能访问 Modrinth Maven 下载 Simply Skills 1.7.2 的编译期元数据；运行时不会把 Simply Skills 打包进本附属。

## License

MIT. See [LICENSE](LICENSE).
