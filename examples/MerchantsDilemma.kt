package examples

import io.github.rmuhamedgaliev.arcana.domain.model.mechanics.ChoiceWeight
import io.github.rmuhamedgaliev.arcana.domain.model.payment.SubscriptionTier
import io.github.rmuhamedgaliev.arcana.domain.model.story.Story
import io.github.rmuhamedgaliev.arcana.dsl.story

/**
 * The Merchant's Dilemma - A complete example game demonstrating all features of the Arcana engine.
 * This game showcases complex branching, character relationships, faction system, world state,
 * consequence chains, skill checks, premium content, multiple endings, and achievement system.
 */
fun createMerchantsDilemma(): Story {
    return story("merchants_dilemma") {
        title {
            en = "The Merchant's Dilemma"
            ru = "Дилемма торговца"
        }
        
        description {
            en = "A tale of commerce, politics, and revolution in the kingdom of Astoria"
            ru = "История торговли, политики и революции в королевстве Астория"
        }
        
        // Initial player attributes
        initialAttributes {
            "gold" to 100
            "reputation" to 50
            "charisma" to 30
            "intelligence" to 40
            "strength" to 25
            "morality" to 50
        }
        
        // NPCs and Relationships
        characters {
            npc("princess_elena") {
                name { 
                    en = "Princess Elena"
                    ru = "Принцесса Елена" 
                }
                faction = "royal_family"
                personality = listOf("intelligent", "ambitious", "suspicious")
                initialRelationship = 0
            }
            
            npc("rebel_marcus") {
                name {
                    en = "Marcus the Rebel" 
                    ru = "Маркус Повстанец"
                }
                faction = "rebels"
                personality = listOf("passionate", "reckless", "charismatic")
                initialRelationship = 0
            }
            
            npc("merchant_guild_master") {
                name {
                    en = "Guild Master Aldric"
                    ru = "Глава гильдии Алдрик"
                }
                faction = "merchant_guild"
                personality = listOf("cunning", "greedy", "pragmatic")
                initialRelationship = 10
            }
        }
        
        // Factions
        factions {
            faction("royal_family") {
                name {
                    en = "Royal Court"
                    ru = "Королевский двор"
                }
                initialStanding = 0
            }
            
            faction("rebels") {
                name {
                    en = "The Resistance"
                    ru = "Сопротивление"
                }
                initialStanding = 0
            }
            
            faction("merchant_guild") {
                name {
                    en = "Merchant Guild"
                    ru = "Торговая гильдия"
                }
                initialStanding = 20
            }
        }
        
        // World State
        worldState {
            political {
                kingdomStability = 60
                taxLevel = 40
                tradeProsperty = 70
            }
            economic {
                inflation = 30
                marketDemand = 80
            }
            social {
                publicMood = 50
                rumorMill = mutableListOf<String>()
            }
        }
        
        // Story Arc 1: The Beginning
        arc("introduction") {
            dependency = null
            
            beat("market_square") {
                text {
                    en = """
                        You are {playerName}, a traveling merchant in the bustling kingdom of Astoria. 
                        The morning sun illuminates the crowded market square where you've set up your stall. 
                        
                        Your current gold: {gold} coins
                        Your reputation: {reputation}/100
                        
                        A hooded figure approaches your stall, glancing nervously around.
                    """.trimIndent()
                    ru = """
                        Вы - {playerName}, странствующий торговец в оживленном королевстве Астория.
                        Утреннее солнце освещает переполненную рыночную площадь, где вы разложили свои товары.
                        
                        Ваше золото: {gold} монет
                        Ваша репутация: {reputation}/100
                        
                        К вашей лавке подходит фигура в капюшоне, нервно оглядываясь по сторонам.
                    """.trimIndent()
                }
                
                choice("listen_carefully") {
                    text {
                        en = "Listen carefully to what they have to say"
                        ru = "Внимательно выслушать, что они хотят сказать"
                    }
                    
                    immediate {
                        target = "intelligence"
                        value = "+1"
                    }
                    
                    goto("mysterious_offer")
                }
                
                choice("ignore_customer") {
                    text {
                        en = "Ignore them and focus on other customers"
                        ru = "Проигнорировать их и заняться другими покупателями"
                    }
                    
                    condition = "morality < 40"
                    
                    immediate {
                        target = "gold"
                        value = "+20"
                    }
                    
                    immediate {
                        target = "reputation"
                        value = "-5"
                    }
                    
                    goto("missed_opportunity")
                }
                
                choice("call_guards") {
                    text {
                        en = "[Suspicious] Call the city guards"
                        ru = "[Подозрительно] Позвать городскую стражу"
                    }
                    
                    condition = "intelligence >= 35 || hasRelationship('royal_family', 20)"
                    
                    immediate {
                        target = "relationship"
                        value = "royal_family,+10"
                    }
                    
                    immediate {
                        target = "relationship"
                        value = "rebels,-20"
                    }
                    
                    goto("loyal_citizen_path")
                }
            }
            
            beat("mysterious_offer") {
                text {
                    en = """
                        The hooded figure whispers urgently: "I represent certain... interested parties. 
                        We need someone with your skills to transport special cargo. 
                        The pay is exceptional - 500 gold coins. But there are risks..."
                        
                        You notice the royal guard patrol is approaching.
                    """.trimIndent()
                    ru = """
                        Фигура в капюшоне шепчет торопливо: "Я представляю определенные... заинтересованные стороны.
                        Нам нужен кто-то с вашими навыками для перевозки особого груза.
                        Плата исключительная - 500 золотых монет. Но есть риски..."
                        
                        Вы замечаете приближающийся патруль королевской стражи.
                    """.trimIndent()
                }
                
                choice("accept_job") {
                    text {
                        en = "Accept the mysterious job"
                        ru = "Принять загадочную работу"
                    }
                    
                    skillCheck("charisma") {
                        difficulty = 15
                        
                        onSuccess {
                            immediate {
                                target = "gold"
                                value = "+500"
                            }
                            
                            immediate {
                                target = "flag"
                                value = "rebel_smuggler"
                            }
                            
                            immediate {
                                target = "relationship"
                                value = "rebel_marcus,+30"
                            }
                            
                            delayed(turns = 3) {
                                target = "event"
                                value = "contraband_discovered"
                            }
                            
                            goto("smuggling_arc")
                        }
                        
                        onFailure {
                            immediate {
                                target = "gold"
                                value = "+100"  // Partial payment
                            }
                            
                            immediate {
                                target = "flag"
                                value = "failed_negotiation"
                            }
                            
                            goto("dangerous_mission")
                        }
                    }
                }
                
                choice("negotiate_price") {
                    text {
                        en = "Try to negotiate a better deal"
                        ru = "Попытаться договориться о лучших условиях"
                    }
                    
                    condition = "charisma >= 25"
                    
                    immediate {
                        target = "gold"
                        value = "+750"  // Better deal
                    }
                    
                    immediate {
                        target = "relationship"
                        value = "rebel_marcus,+20"
                    }
                    
                    immediate {
                        target = "flag"
                        value = "skilled_negotiator"
                    }
                    
                    goto("premium_smuggling_arc")  // Premium content
                    requiresSubscription = "PREMIUM"
                }
                
                choice("refuse_politely") {
                    text {
                        en = "Politely decline the offer"
                        ru = "Вежливо отклонить предложение"
                    }
                    
                    immediate {
                        target = "morality"
                        value = "+5"
                    }
                    
                    immediate {
                        target = "reputation"
                        value = "+10"
                    }
                    
                    goto("honest_merchant_path")
                }
            }
            
            // Premium content example
            beat("premium_smuggling_arc") {
                requiresSubscription = "PREMIUM"
                
                text {
                    en = """
                        [PREMIUM CONTENT]
                        Your negotiation skills impress the mysterious contact. They reveal more details:
                        "You're not just smuggling goods - you're carrying messages between rebel cells.
                        This could change the fate of the kingdom..."
                    """.trimIndent()
                    ru = """
                        [ПРЕМИУМ КОНТЕНТ]
                        Ваши навыки переговоров впечатляют таинственного контакта. Они раскрывают больше деталей:
                        "Вы не просто перевозите товары - вы передаете сообщения между ячейками повстанцев.
                        Это может изменить судьбу королевства..."
                    """.trimIndent()
                }
                
                // More premium content choices would go here
                choice("accept_mission") {
                    text {
                        en = "Accept this dangerous but lucrative mission"
                        ru = "Принять эту опасную, но выгодную миссию"
                    }
                    
                    immediate {
                        target = "flag"
                        value = "premium_rebel_contact"
                    }
                    
                    goto("rebel_headquarters")
                }
            }
        }
        
        // Story Arc 2: The Choice of Allegiance
        arc("allegiance_choice") {
            dependency = "hasFlag('rebel_smuggler') OR hasFlag('loyal_citizen')"
            exclusiveWith = "merchant_focus_arc"
            
            beat("crossroads") {
                text {
                    en = """
                        Three months have passed. Your actions have not gone unnoticed.
                        
                        {if hasFlag('rebel_smuggler')}
                        Princess Elena herself requests an audience. She knows about your rebel connections.
                        {else}
                        Your loyalty to the crown has been noted. You receive an invitation to the royal court.
                        {endif}
                        
                        Current relationships:
                        - Princess Elena: {relationship_princess_elena}
                        - Rebel Marcus: {relationship_rebel_marcus}
                        - Guild Master: {relationship_merchant_guild_master}
                        
                        Kingdom stability: {worldState.political.kingdomStability}%
                    """.trimIndent()
                    ru = """
                        Прошло три месяца. Ваши действия не остались незамеченными.
                        
                        {if hasFlag('rebel_smuggler')}
                        Сама принцесса Елена просит аудиенции. Она знает о ваших связях с повстанцами.
                        {else}
                        Ваша верность короне была отмечена. Вы получаете приглашение в королевский двор.
                        {endif}
                        
                        Текущие отношения:
                        - Принцесса Елена: {relationship_princess_elena}
                        - Повстанец Маркус: {relationship_rebel_marcus}
                        - Глава гильдии: {relationship_merchant_guild_master}
                        
                        Стабильность королевства: {worldState.political.kingdomStability}%
                    """.trimIndent()
                }
                
                // Multiple complex choices would go here based on accumulated state
                choice("side_with_royals") {
                    text {
                        en = "Pledge loyalty to the crown"
                        ru = "Присягнуть на верность короне"
                    }
                    
                    condition = "!hasFlag('rebel_leader')"
                    
                    immediate {
                        target = "relationship"
                        value = "royal_family,+30"
                    }
                    
                    immediate {
                        target = "relationship"
                        value = "rebels,-50"
                    }
                    
                    goto("royal_path")
                }
                
                choice("side_with_rebels") {
                    text {
                        en = "Secretly support the rebellion"
                        ru = "Тайно поддержать восстание"
                    }
                    
                    condition = "!hasFlag('royal_advisor')"
                    
                    immediate {
                        target = "relationship"
                        value = "rebels,+30"
                    }
                    
                    immediate {
                        target = "relationship"
                        value = "royal_family,-30"
                    }
                    
                    goto("rebel_path")
                }
                
                choice("remain_neutral") {
                    text {
                        en = "Maintain neutrality and focus on trade"
                        ru = "Сохранить нейтралитет и сосредоточиться на торговле"
                    }
                    
                    immediate {
                        target = "gold"
                        value = "+200"
                    }
                    
                    immediate {
                        target = "flag"
                        value = "neutral_merchant"
                    }
                    
                    goto("merchant_path")
                }
            }
        }
        
        // Multiple Endings
        endings {
            ending("merchant_emperor") {
                requirements {
                    gold >= 10000 &&
                    hasRelationship("merchant_guild", 80) &&
                    worldState.economic.tradeProsperty >= 90
                }
                
                rarity = EndingRarity.LEGENDARY  // Only 2% of players achieve this
                category = EndingCategory.POWER
                
                text {
                    en = "You have become the most powerful merchant in the kingdom, controlling trade routes and influencing politics from the shadows."
                    ru = "Вы стали самым влиятельным торговцем в королевстве, контролируя торговые пути и влияя на политику из тени."
                }
                
                unlocks {
                    newGamePlus = true
                    characterBonus = "golden_touch"
                    achievementId = "trade_emperor"
                }
            }
            
            ending("revolutionary_hero") {
                requirements {
                    hasRelationship("rebels", 70) &&
                    worldState.political.kingdomStability <= 30 &&
                    hasFlag("led_revolution")
                }
                
                rarity = EndingRarity.RARE  // 15% of players
                category = EndingCategory.HEROIC
                
                text {
                    en = "You led the revolution that toppled the corrupt monarchy. The people hail you as a hero of freedom."
                    ru = "Вы возглавили революцию, свергнувшую коррумпированную монархию. Народ провозглашает вас героем свободы."
                }
            }
            
            ending("royal_advisor") {
                requirements {
                    hasRelationship("princess_elena", 60) &&
                    hasRelationship("royal_family", 50) &&
                    intelligence >= 70
                }
                
                rarity = EndingRarity.UNCOMMON  // 30% of players
                category = EndingCategory.DIPLOMATIC
                
                text {
                    en = "Your wisdom and loyalty have earned you a place as the royal advisor. You help guide the kingdom toward prosperity."
                    ru = "Ваша мудрость и верность заслужили вам место королевского советника. Вы помогаете вести королевство к процветанию."
                }
            }
            
            ending("tragic_downfall") {
                requirements {
                    gold <= 0 &&
                    reputation <= 10
                }
                
                rarity = EndingRarity.COMMON  // 40% of players who make poor choices
                category = EndingCategory.TRAGIC
                
                text {
                    en = "Your business failed, your reputation ruined. You leave Astoria in disgrace, seeking a new beginning elsewhere."
                    ru = "Ваш бизнес потерпел крах, ваша репутация разрушена. Вы покидаете Асторию в позоре, ища новое начало в другом месте."
                }
            }
            
            ending("balanced_life") {
                requirements {
                    gold in 1000..5000 &&
                    reputation >= 50 &&
                    morality >= 60
                }
                
                rarity = EndingRarity.COMMON  // Most common good ending
                category = EndingCategory.PEACEFUL
                
                text {
                    en = "You've built a respectable trading business and are known as a fair and honest merchant throughout the kingdom."
                    ru = "Вы построили уважаемый торговый бизнес и известны как справедливый и честный торговец по всему королевству."
                }
            }
        }
        
        // Achievements
        achievements {
            achievement("first_deal") {
                name { 
                    en = "First Deal"
                    ru = "Первая сделка" 
                }
                description { 
                    en = "Complete your first transaction"
                    ru = "Завершите свою первую сделку" 
                }
                condition = "gold > initialGold"
            }
            
            achievement("silver_tongue") {
                name { 
                    en = "Silver Tongue"
                    ru = "Серебряный язык" 
                }
                description { 
                    en = "Successfully negotiate 10 deals"
                    ru = "Успешно проведите 10 переговоров" 
                }
                condition = "getStatistic('successful_negotiations') >= 10"
            }
            
            achievement("kingmaker") {
                name { 
                    en = "Kingmaker"
                    ru = "Создатель королей" 
                }
                description { 
                    en = "Influence the fate of the kingdom"
                    ru = "Повлияйте на судьбу королевства" 
                }
                condition = "hasFlag('influenced_politics')"
                rarity = AchievementRarity.LEGENDARY
            }
        }
    }
}
